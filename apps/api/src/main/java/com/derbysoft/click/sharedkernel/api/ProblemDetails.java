package com.derbysoft.click.sharedkernel.api;

import java.net.URI;

public record ProblemDetails(
    URI type,
    String title,
    int status,
    String detail,
    String instance
) {}
