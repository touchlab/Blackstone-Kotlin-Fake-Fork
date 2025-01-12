package com.blackstone.fake.providers

import com.blackstone.fake.Fake.Companion.fake
import com.blackstone.fake.providers.definition.BookProvider

class BookProviderImp : BaseProvider(), BookProvider {
    override val title: String
        get() = getValue("title") { fake.fetch("book.title") }
    override val author: String
        get() = getValue("author") { fake.fetch("book.author") }
    override val publisher: String
        get() = getValue("publisher") { fake.fetch("book.publisher") }
    override val genre: String
        get() = getValue("genre") { fake.fetch("book.genre") }
}