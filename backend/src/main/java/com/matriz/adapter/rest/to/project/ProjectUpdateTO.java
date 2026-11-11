package com.matriz.adapter.rest.to.project;

import java.util.Optional;

/**
 * Absent field = unchanged ({@code null}); {@code "description": null} clears it ({@code Optional.empty()}).
 * A class, not a record: Jackson would turn absent record components into {@code Optional.empty()}.
 */
public class ProjectUpdateTO {

    public Optional<String> name;
    public Optional<String> description;
}
