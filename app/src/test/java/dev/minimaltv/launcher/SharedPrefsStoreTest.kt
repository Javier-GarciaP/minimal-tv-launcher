package dev.minimaltv.launcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Checks that the launcher preferences survive and behave as expected.
 *
 * The store is created twice over the same preferences to prove the values are read back
 * from storage instead of being cached in memory.
 */
class SharedPrefsStoreTest {

    private val prefs = FakeSharedPreferences()

    @Test
    fun `defaults are mountains and medium text`() {
        val store = SharedPrefsStore(prefs)

        assertEquals(SharedPrefsStore.MODE_MOUNTAINS, store.wallpaperMode)
        assertEquals(SharedPrefsStore.DEFAULT_FONT_SCALE, store.fontScale, 0.0001f)
        assertTrue(store.featuredPackages.isEmpty())
    }

    @Test
    fun `featured packages survive a new store instance`() {
        SharedPrefsStore(prefs).featuredPackages = setOf("com.netflix", "com.youtube.tv")

        val reopened = SharedPrefsStore(prefs)

        assertEquals(setOf("com.netflix", "com.youtube.tv"), reopened.featuredPackages)
    }

    @Test
    fun `wallpaper mode survives a new store instance`() {
        SharedPrefsStore(prefs).wallpaperMode = SharedPrefsStore.MODE_SOLID

        assertEquals(SharedPrefsStore.MODE_SOLID, SharedPrefsStore(prefs).wallpaperMode)
    }

    @Test
    fun `font scale survives a new store instance`() {
        SharedPrefsStore(prefs).fontScale = 1.2f

        assertEquals(1.2f, SharedPrefsStore(prefs).fontScale, 0.0001f)
    }

    @Test
    fun `featured packages can be emptied again`() {
        val store = SharedPrefsStore(prefs)
        store.featuredPackages = setOf("com.netflix")

        store.featuredPackages = emptySet()

        assertTrue(SharedPrefsStore(prefs).featuredPackages.isEmpty())
    }

    @Test
    fun `the featured only filter is off by default and survives a new store instance`() {
        assertFalse(SharedPrefsStore(prefs).showFeaturedOnly)

        SharedPrefsStore(prefs).showFeaturedOnly = true

        assertTrue(SharedPrefsStore(prefs).showFeaturedOnly)
    }
}
