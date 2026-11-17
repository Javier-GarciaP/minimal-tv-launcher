package dev.minimaltv.launcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.Collator
import java.util.Locale

/**
 * Checks how the home screen list is composed.
 *
 * Every test states what it protects: the order of the rows, where the featured group
 * goes and what happens when the user has not featured anything.
 */
class AppListBuilderTest {

    private val spanish = Collator.getInstance(Locale("es")).apply {
        strength = Collator.SECONDARY
    }

    private fun entry(pkg: String, label: String) = AppEntry(pkg, label, isSystem = false)

    private fun build(
        entries: List<AppEntry>,
        featured: Set<String> = emptySet(),
        ownPackage: String = "dev.minimaltv.launcher",
        showFeaturedOnly: Boolean = false,
    ) = AppListBuilder.build(entries, featured, ownPackage, showFeaturedOnly, spanish)

    @Test
    fun `settings entry is always the last row`() {
        val rows = build(listOf(entry("a", "Alfa"), entry("b", "Beta")))

        assertEquals(Row.Settings, rows.last())
    }

    @Test
    fun `no header is shown when nothing is featured`() {
        val rows = build(listOf(entry("a", "Alfa"), entry("b", "Beta")))

        assertTrue(rows.none { it is Row.Header })
        assertEquals(3, rows.size)
    }

    @Test
    fun `featured apps are grouped under a header at the top`() {
        val rows = build(
            entries = listOf(entry("a", "Alfa"), entry("b", "Beta"), entry("c", "Ceta")),
            featured = setOf("c"),
        )

        assertEquals(Row.Header(AppListBuilder.HEADER_FEATURED), rows[0])
        assertEquals("c", (rows[1] as Row.App).entry.packageName)
        assertTrue((rows[1] as Row.App).featured)
        assertEquals(Row.Header(AppListBuilder.HEADER_OTHERS), rows[2])
        assertFalse((rows[3] as Row.App).featured)
    }

    @Test
    fun `the rest of the apps get their own header so nothing looks featured by mistake`() {
        val rows = build(
            entries = listOf(entry("a", "Alfa"), entry("b", "Beta"), entry("c", "Ceta")),
            featured = setOf("c"),
        )

        assertEquals(Row.Header(AppListBuilder.HEADER_OTHERS), rows[2])
    }

    @Test
    fun `no second header when every app is featured`() {
        val rows = build(
            entries = listOf(entry("a", "Alfa"), entry("b", "Beta")),
            featured = setOf("a", "b"),
        )

        assertTrue(rows.none { it is Row.Header && it.title == AppListBuilder.HEADER_OTHERS })
    }

    @Test
    fun `no second header when nothing is featured`() {
        val rows = build(entries = listOf(entry("a", "Alfa"), entry("b", "Beta")))

        assertTrue(rows.none { it is Row.Header })
    }

    @Test
    fun `featured apps keep the alphabetical order among themselves`() {
        val rows = build(
            entries = listOf(entry("a", "Alfa"), entry("b", "Beta"), entry("c", "Ceta")),
            featured = setOf("a", "c"),
        )

        val featuredLabels = rows.filterIsInstance<Row.App>()
            .filter { it.featured }
            .map { it.entry.label }

        assertEquals(listOf("Alfa", "Ceta"), featuredLabels)
    }

    @Test
    fun `apps are sorted alphabetically`() {
        val rows = build(listOf(entry("c", "Ceta"), entry("a", "Alfa"), entry("b", "Beta")))

        val labels = rows.filterIsInstance<Row.App>().map { it.entry.label }

        assertEquals(listOf("Alfa", "Beta", "Ceta"), labels)
    }

    @Test
    fun `accents sort next to their base letter`() {
        val rows = build(listOf(entry("1", "Zeta"), entry("2", "Ángel"), entry("3", "Ana")))

        val labels = rows.filterIsInstance<Row.App>().map { it.entry.label }

        assertEquals(listOf("Ana", "Ángel", "Zeta"), labels)
    }

    @Test
    fun `the launcher itself is never listed`() {
        val rows = build(
            entries = listOf(
                entry("dev.minimaltv.launcher", "Minimal TV"),
                entry("a", "Alfa"),
            ),
        )

        val labels = rows.filterIsInstance<Row.App>().map { it.entry.label }

        assertEquals(listOf("Alfa"), labels)
    }

    @Test
    fun `only the featured apps are listed when the filter is on`() {
        val rows = build(
            entries = listOf(entry("a", "Alfa"), entry("b", "Beta"), entry("c", "Ceta")),
            featured = setOf("c"),
            showFeaturedOnly = true,
        )

        val labels = rows.filterIsInstance<Row.App>().map { it.entry.label }

        assertEquals(listOf("Ceta"), labels)
        assertEquals(Row.Settings, rows.last())
    }

    @Test
    fun `settings stays reachable when the filter is on and nothing is featured`() {
        val rows = build(
            entries = listOf(entry("a", "Alfa")),
            featured = emptySet(),
            showFeaturedOnly = true,
        )

        assertEquals(listOf(Row.Settings), rows)
    }
}
