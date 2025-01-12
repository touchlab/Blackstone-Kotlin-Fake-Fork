import com.blackstone.fake.Fake
import com.blackstone.fake.readResource
import com.blackstone.fake.shuffle
import kotlin.math.sqrt
import kotlin.test.*


class TestFake {

    @Test
    fun `it should provide a fake name`() {
        val name = Fake.name().name
        assertNotNull(name)
        assertTrue(name.split(" ").size == 2)
    }

    @Test
    fun `it should provide a fake first name`() {
        val fistName = Fake.name().firstName
        assertNotNull(fistName)
    }

    @Test
    fun `it should provide an address`() {
        val streetAddress = Fake.address().streetAddress
        val city = Fake.address().city
        val state = Fake.address().state
        val stateAbrev = Fake.address().stateAbbreviation
        val zip = Fake.address().zipCode

        assertNotNull(streetAddress)
        assertNotNull(city)
        assertNotNull(state)
        assertNotNull(stateAbrev)
        assertNotNull(zip)
    }


    @Test
    fun `it should create a formatted date`() {
        val date = Fake.dateTime().dateFormatter

        assertNotNull(date)
        assertPatternMatches(date, "[0-9]{2}/[0-9]{2}/[0-9]{4}")
    }


    @Test
    fun `it should return demographic data`() {
        val race = Fake.demographic().race
        val sex = Fake.demographic().sex
        val maritalStatus = Fake.demographic().maritalStatus

        assertNotNull(race)
        assertNotNull(maritalStatus)
        assertNotNull(sex)
    }

    @Test
    fun `it should return file data`() {
        val mimeType = Fake.file().mimeType
        val extension = Fake.file().extension

        assertNotNull(mimeType)
        assertNotNull(extension)
        assertPatternMatches(mimeType, "[a-z]+/[a-z]+")
        assertPatternMatches(extension, "[a-zA-Z]+")
    }

    @Test
    fun `it should provide a phone number`() {
        val phoneNumber = Fake.phone().formats()

        assertNotNull(phoneNumber)
    }

    @Test
    fun `it should provide a fake sentence`() {
        val text = Fake.lorem().sentence()

        assertNotNull(text)
        assertTrue(text.split(" ").size > 5)
    }

    @Test
    fun `it should generate more than one sentence`(){
        val paragraph = Fake.lorem().paragraph()

        assertNotNull(paragraph)
        assertTrue(paragraph.split(".").size > 2)
    }


    @Test
    fun `it should create an MD5 hash`(){
        val hash = Fake.misc().md5
        val hash2 = Fake.misc().md5

        assertNotNull(hash)
        assertNotEquals(hash, hash2)
        assertEquals(32, hash.length)
    }

    @Test
    fun `it should create a UUID`() {
        val uuid = Fake.uuid().uuid

        assertNotNull(uuid)
    }

    @Test
    fun `it should create a sha1 hash`(){
        val hash = Fake.misc().sha1
        val hash2 = Fake.misc().sha1

        assertNotNull(hash)
        assertNotEquals(hash, hash2)
        assertEquals(40, hash.length)
    }

    @Test
    fun `it should create a sha256 hash`(){
        val hash = Fake.misc().sha256
        val hash2 = Fake.misc().sha256

        assertNotNull(hash)
        assertNotEquals(hash, hash2)
        assertEquals(64, hash.length)
    }


    @Test
    fun `it should create an isbn10 code`() {
        val isbn = Fake.barcode().isbn10
        val isbn1 = Fake.barcode().isbn10

        assertNotNull(isbn)
        assertNotNull(isbn1)
        assertNotEquals(isbn, isbn1)
        assertEquals(10, isbn.length)
    }

    @Test
    fun `it should create an ISBN13 code`(){
        val isbn = Fake.barcode().isbn13
        val isbn1 = Fake.barcode().isbn13

        assertNotNull(isbn)
        assertNotNull(isbn1)
        assertNotEquals(isbn, isbn1)
        assertEquals(13, isbn.length)
    }

    @Ignore
    @Test
    fun `it should return a biased number`(){
        val biasedNumber = Fake.biased().biasedNumberBetween { sqrt(it) }
        val biasedNumber2 = Fake.biased().biasedNumberBetween { sqrt(it) }

        assertNumberBetween(biasedNumber, 10.0, 20.0)
        assertNotEquals(biasedNumber, biasedNumber2)
    }

    @Test
    fun `it should shuffle string`(){
        val t = "somethingreallylong".shuffle()
        assertNotEquals(t, "somethingreallylong")
    }

    @Test
    fun `it should produce a hex color code`(){
        val t = Fake.color().hexColor
        assertNotNull(t)
        assertLength(t, 7)
        assertTrue(t.startsWith("#"))
    }

    @Test
    fun `it should produce a safe hex color code`(){
        val t = Fake.color().safeHexColor
        assertNotNull(t)
        assertLength(t, 7)
        assertTrue(t.startsWith("#"))
    }

    @Test
    fun `it should generate an RGB value`(){
        val t = Fake.color().cssRgbColor
        assertTrue(t.startsWith("rgb("))
        assertNotNull(t)
    }

    @Test
    fun `it should generate an RGBA value`(){
        val t =  Fake.color().rgbaCssColor

        assertTrue(t.startsWith("rgba("))
        assertNotNull(t)
    }

    @Test
    fun `it should return a color string`(){
        val colorStr = Fake.color().colorName

        assertTrue(colorStr.length > 3, "color string: $colorStr was not longer than 3 characters")
    }


    @Test
    fun `it should get resource`(){
        val t = readResource(TestFake::class, "/")

        assertNotNull(t)
    }


    private fun assertLength(str: String, expectedLength: Int) {
        assertEquals(str.length, expectedLength, "String: $str is ${str.length}.. not $expectedLength")
    }

    private fun assertNumberBetween(numberToBeTested: Double, min: Double, max: Double, message: String = "") {
        assertTrue (numberToBeTested in min..max, "Number: $numberToBeTested was not in range $min to $max. $message")
    }

    private fun assertPatternMatches(text: String, pattern: String, message: String = "") {
        assertTrue(Regex(pattern).matches(text), "the pattern $pattern did not match $text. $message")
    }
}

