package com.example.ktor.resources

import io.ktor.resources.Resource

@Resource(path = "foo/bar/kotlinxio")
public data object BazKotlinxIO

@Resource(path = "foo/bar/streams")
public data object BazStream

@Resource(path = "foo/bar/two")
public data object BazTwo

@Resource(path = "foo/bar/noOutputs")
public data object BazNoOutputs
