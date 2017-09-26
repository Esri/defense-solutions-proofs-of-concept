# -*- coding: utf-8 -*-
import mds.constants
import mds.date_time
### import mds.ordered_set
import mds.netcdf.file


def copy(
        dataset,
        variable_names,
        output_filename,
        extent=None,
        dimension_selections=[],
        value_selection_method=mds.constants.SELECT_BY_VALUE,
        history_message=None):
    """
    Copy the variables *variable_names* from *dataset* to a
    netCDF file named *output_filename*, honoring the spatial *extent* and
    *dimension_selections* passed in. If no spatial variable is selected,
    the value of *extent* is discarded.

    The *extent* passed in must be an sequence containing
    ``[x_min, y_min, x_max, y_max]``. If not provided, the full extent is
    copied.

    The *dimension_selections* passed in must be an iterable of sequences
    containing ``[dimension_name, start_value, end_value]``. Dimensions
    not present in *dimension_selections* will be copied in full.

    The interpretation of the ``start_value`` and ``end_value`` stored in
    each dimension selection depends on the value of
    *value_selection_method*. This argument must be one of the
    selection methods defined in :mod:`mds`.

    The *history_message* is written to the netCDF file. The value is
    appended to the value of the global history attribute. If no value is
    passed, the history attribute, if present, is not changed.
    """
    assert len(variable_names) > 0
    assert all([variable_name in dataset.data_variable_names() for
        variable_name in variable_names])

    variable_names = list(variable_names)
    first_spatial_variable_name = next((variable_name for variable_name in
        variable_names if dataset.is_spatial_variable(variable_name)),
        None)

    if extent is None and not first_spatial_variable_name is None:
        # No extent passed in. Use the extent of the first spatial variable
        # selected.
        extent = dataset.extent(first_spatial_variable_name)
    elif extent is not None and first_spatial_variable_name is None:
        # None of the selected variables is spatial.
        extent = None

    if dimension_selections is None:
        dimension_selections = []

    with mds.netcdf.file.initialize_dataset_copy(dataset, output_filename) as \
            new_dataset:
        if history_message is not None:
            mds.netcdf.file.append_history_message(new_dataset, history_message)

        variable_names += mds.netcdf.file.dependent_variable_names(dataset,
            variable_names)
        dimension_names = mds.netcdf.file.dependent_dimension_names(dataset,
            variable_names)

        # Dictionary with slice by dimension name.
        # Initialize the slices by the full range of values.
        dimension_slices = {dimension_name: (0,
            len(dataset.dataset.dimensions[dimension_name])) for
                dimension_name in dimension_names}

        if not first_spatial_variable_name is None:
            # Add slice of spatial dimensions.
            assert not extent is None
            dimension_slices.update(dataset.spatial_dimension_slices(
                first_spatial_variable_name, extent))
        assert all([dimension_name in dimension_names for dimension_name in
            dimension_slices.keys()])

        # Update (non-spatial) dimensions with user defined slicing settings.
        for dimension_selection in dimension_selections:
            dimension_name, start_value, end_value = dimension_selection

            if dimension_name in dataset.variable_names():
                dimension_variable = dataset.variable(dimension_name)

                if value_selection_method == mds.SELECT_BY_VALUE and \
                        dataset.convention.is_time_dimension_variable(
                            dimension_variable):
                    # User passed in iso formatted date/time strings. Convert
                    # these to datetime instances and subsequently to dimension
                    # coordinates.
                    start_value = mds.date_time.from_iso_format(start_value)
                    end_value = mds.date_time.from_iso_format(end_value)
                    time_variable = dataset.variable(dimension_name)
                    start_value, end_value = mds.netcdf.dates_to_coordinates([
                        start_value, end_value], time_variable)

            dimension_slices[dimension_name] = dataset.dimension_slice(
                dimension_name, start_value, end_value, value_selection_method)

        ### # About to write dimensions and variables. First order variable and
        ### # dimension names like in the source dataset.
        ### dimension_names = list(dimension_names)
        ### variable_names = list(mds.ordered_set.order(set(variable_names),
        ###     dataset.variable_names()))

        # Create dimensions.
        for dimension_name in dimension_names:
            new_dataset.createDimension(dimension_name,
                dimension_slices[dimension_name][1] -
                    dimension_slices[dimension_name][0] if not
                        dataset.dimension(dimension_name).isunlimited() else \
                            None)

        def copy_variable(
                variable,
                new_dataset,
                variable_name):
            new_variable = mds.netcdf.file.init_variable(variable, new_dataset,
                variable_name)

            # When copying, there is no need to scale the values. It is
            # better not to because it results in small differences due to
            # casting.
            variable.set_auto_maskandscale(False)
            new_variable.set_auto_maskandscale(False)

            slices_ = [slice(*dimension_slices[dimension_name]) for
                dimension_name in variable.dimensions]

            new_variable[:] = variable[slices_] if slices_ else variable[:]

        for dimension_name in dimension_names:
            if dimension_name in dataset.dataset.variables:
                variable = dataset.dataset.variables[dimension_name]
                copy_variable(variable, new_dataset, dimension_name)

        for variable_name in variable_names:
            if variable_name not in dimension_names:
                variable = dataset.dataset.variables[variable_name]
                copy_variable(variable, new_dataset, variable_name)
