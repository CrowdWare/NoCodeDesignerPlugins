package at.crowdware.nocode.utils

data class App(
    val name: String,
    val pages: List<Page>
)

data class Page(
    val title: String,
    val elements: List<Element>
)

data class Element(
    val id: String
)