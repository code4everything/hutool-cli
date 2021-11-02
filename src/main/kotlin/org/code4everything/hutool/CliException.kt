package org.code4everything.hutool

class CliException : RuntimeException {

    constructor() : super("hutool-cli exception test in debug mode")
    constructor(msg: String?) : super(msg)
}
