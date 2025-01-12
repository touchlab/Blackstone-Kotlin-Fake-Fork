package com.blackstone.fake

import net.mamoe.yamlkt.Yaml
import com.blackstone.fake.providers.*
import com.blackstone.fake.providers.definition.*
import kotlin.native.concurrent.ThreadLocal
import kotlin.random.Random


class Fake {
    private val numeralAndBracesRegEx = "#\\{(.*?)\\}"
    private val numeralRegEx = ".*#(\\{[^a-zA-z]|[^{])+"
    private val numeralOnlyRegEx = "#"
    private val defaultLanguage = "en"

    var stringLocale = "en"

    val random = Random.Default

    private val yaml = Yaml()

    private val fakeValues: LinkedHashMap<String, LinkedHashMap<String, String>> = getValues(stringLocale)
    private val fakeValuesDefaults: LinkedHashMap<String, LinkedHashMap<String, String>> = getValues(defaultLanguage)

    var uniqueValueActive = false


    private fun getValues(stringLocale: String): LinkedHashMap<String, LinkedHashMap<String, String>> {
        val yamlFile = readResource(Fake::class, "/en.yml")

//        val yamlFile = FileSystem.readFile(path, ContentEncoding.Utf8)!!

        val yamlValuesDefault = yaml.decodeMapFromString(yamlFile) as Map<*, *>
        val localeValuesDefault = yamlValuesDefault[stringLocale] as Map<*, *>
        return localeValuesDefault["faker"] as LinkedHashMap<String, LinkedHashMap<String, String>>
    }


    private fun checkAndSetParams(key: String, baseParams: Params, params: Params) {
        if (!params.check || this.fakeValuesDefaults.size == 0) {
            throw Exception(getResourceNotFound(key))
        }
        params.separator = baseParams.category.indexOf(".")
        params.category = baseParams.category
        params.values = this.fakeValuesDefaults
        params.check = false
    }

    private fun getCategoryAndValues(key: String, baseParams: Params): Params {
        val params = Params(baseParams.separator, baseParams.category, baseParams.check, baseParams.values)

        if (params.separator == -1 && params.values[params.category] == null) {
            checkAndSetParams(key, baseParams, params)
        }

        while (params.separator != -1) {
            if (params.check && params.values[params.category.substring(0, params.separator)] == null) {
                checkAndSetParams(key, baseParams, params)
            }
            params.values = params.values[params.category.substring(
                0,
                params.separator
            )] as LinkedHashMap<String, LinkedHashMap<String, String>>
            params.category = params.category.substring(params.separator + 1, params.category.length)
            params.separator = params.category.indexOf(".")
            if (params.separator == -1 && params.values[params.category] == null) {
                checkAndSetParams(key, baseParams, params)
            }
        }
        return params
    }

    private fun fetchCategory(
        key: String, category: String, check: Boolean,
        valuesToFetch: LinkedHashMap<String, LinkedHashMap<String, String>>
    ): LinkedHashMap<*, *> {
        val (_, subCategory, _, values) = getCategoryAndValues(
            key,
            Params(category.indexOf("."), category, check, valuesToFetch)
        )
        return when {
            values[subCategory] is LinkedHashMap<*, *> -> values[subCategory] as LinkedHashMap<*, *>
            values[subCategory] is ArrayList<*> -> {
                val valuesList = values[subCategory] as ArrayList<LinkedHashMap<*, *>>
                valuesList[random.nextInt(valuesList.size)]
            }
            else -> throw Exception("Resource Key not found $category on $key")
        }
    }

    private fun fetchSelectedValue(key: String, category: String, selected: String): String {
        var categoryValues = fetchCategory(key, category, true, this.fakeValues)
        if (categoryValues[selected] == null) {
            if (this.fakeValuesDefaults.size == 0) {
                throw Exception(getResourceNotFound(key))
            }
            categoryValues = fetchCategory(key, category, false, this.fakeValuesDefaults)
            if (categoryValues[selected] == null) {
                throw Exception(getResourceNotFound(key))
            }
        }
        when {
            categoryValues[selected] is ArrayList<*> -> {
                val values = categoryValues[selected] as ArrayList<ArrayList<String>>
                if (values[0] is CharSequence) {
                    return getRandomString(values as ArrayList<String>)
                }
                return getRandomString(values[random.nextInt(values.size)])
            }
            categoryValues[selected] is String -> return categoryValues[selected] as String
            else -> throw Exception("Resource $category.$selected is not a value")
        }
    }

    private fun matchAndReplace(
        stringToMatch: String,
        regExp: String,
        transform: (MatchResult) -> CharSequence
    ): String {
        val matcher = Regex(regExp)
        return matcher.replace(stringToMatch, transform)
    }

    private fun fetchKeyValueData(category: String, selectedValue: String): String {
        return matchAndReplace(selectedValue, numeralAndBracesRegEx) {
            fetchValueByCategory(category, it.groupValues.last())
        }
    }

    private fun fetchValueByCategory(category: String, key: String): String {
        val separator = key.lastIndexOf(".")
        var dataCategory = category
        var keyToFetch = key
        var result: String

        if (separator != -1) {
            dataCategory = key.substring(0, separator).toLowerCase()
            keyToFetch = key.substring(separator + 1, key.length)
            result = fetchSelectedValue(key, dataCategory, keyToFetch)
        } else {
            val categoryValues = fakeValues[dataCategory] as? LinkedHashMap<String, ArrayList<String>>
                ?: fakeValuesDefaults[dataCategory] as LinkedHashMap<String, ArrayList<String>>
            val selectedValues = categoryValues[keyToFetch] as ArrayList<String>
            result = getRandomString(selectedValues)
        }

        if (result.matches(Regex(numeralRegEx))) {
            result = fetchNumerals(result)
        }
        if (result.matches(Regex(numeralAndBracesRegEx))) {
            result = fetchKeyValueData(dataCategory, result)
        }
        return result
    }

    private fun fetchNumerals(numeral: String): String {
        return matchAndReplace(
            numeral, numeralOnlyRegEx
        ) { random.nextInt(10).toString() }
    }

    fun fetch(key: String): String {
        val separator = key.lastIndexOf(".")
        val category = key.substring(0, separator)
        val selected = key.substring(separator + 1, key.length)
        val selectedValue = fetchSelectedValue(key, category, selected)

        return when {
            selectedValue.matches(Regex(numeralAndBracesRegEx)) -> fetchKeyValueData(category, selectedValue)
            selectedValue.matches(Regex(numeralRegEx)) -> fetchNumerals(selectedValue)
            else -> selectedValue
        }
    }

    private fun getRandomString(selectedValues: ArrayList<String>): String =
        selectedValues[random.nextInt(selectedValues.size)]

    data class Params(
        var separator: Int, var category: String, var check: Boolean,
        var values: LinkedHashMap<String, LinkedHashMap<String, String>>
    )

    private fun getResourceNotFound(key: String): String = "Resource not found $key"

    @ThreadLocal
    companion object Companion {
        var fake: Fake = Fake()

        private val providers = HashMap<String, Provider>()

        fun getUniqueValue(): Boolean = fake.uniqueValueActive

        fun changeUniqueValueState() {
            fake.uniqueValueActive = !fake.uniqueValueActive
        }

        fun getProvider(key: String, provider: () -> Provider): Provider {
            return providers[key] ?: run {
                val auxProvider = provider()
                providers[key] = auxProvider
                auxProvider
            }
        }

        fun random(): Random { return fake.random }
        fun address(): AddressProvider = getProvider("address") { AddressProviderImp() } as AddressProviderImp
        fun barcode(): BarcodeProvider = getProvider("barcode") { BarcodeProviderImp() } as BarcodeProvider
        fun biased(): BiasedProvider = getProvider("biased") { BiasedProviderImp() } as BiasedProvider
        fun book(): BookProvider = getProvider("book") { BookProviderImp() } as BookProvider
        fun color(): ColorProvider = getProvider("color") { ColorProviderImp() } as ColorProvider
        fun company(): CompanyProvider = getProvider("company") { CompanyProviderImp() } as CompanyProvider
        fun compass(): CompassProvider = getProvider("compass") { CompassProviderImp() } as CompassProvider
        fun dateTime(): DateTimeProvider = getProvider("dateTime") { DateTimeProviderImp() } as DateTimeProvider
        fun demographic(): DemographicProvider = getProvider("demographic") { DemographicProviderImp() } as DemographicProvider
        fun file(): FileProvider = getProvider("file") { FileProviderImp() } as FileProvider
        fun food(): FoodProvider = getProvider("food") { FoodProviderImp() } as FoodProvider
        fun image(): ImageProvider = getProvider("image") { ImageProviderImp() } as ImageProvider
        fun internet(): InternetProvider = getProvider("internet") { InternetProviderImp() } as InternetProvider
        fun job(): JobProvider = getProvider("job") { JobProviderImp() } as JobProvider
        fun lorem(): LoremProvider = getProvider("lorem") { LoremProviderImp() } as LoremProvider
        fun misc(): MiscellaneousProvider = getProvider("misc") { MiscellaneousProviderImp() } as MiscellaneousProvider
        fun name(): NameProvider = getProvider("name") { NameProviderImp() } as NameProvider
        fun phone(): PhoneNumberProvider = getProvider("phone") { PhoneNumberProviderImp() } as PhoneNumberProvider
        fun uuid(): UuidProvider = getProvider("uuid") { UuidProviderImp() } as UuidProvider
    }
}



