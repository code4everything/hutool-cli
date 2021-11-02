package org.code4everything.hutool

/**
 * @author pantao
 * @since 2020/10/29
 */
class CliException : RuntimeException {

    constructor() : super("hutool-cli exception test in debug mode")
    constructor(msg: String?) : super(msg)
}
