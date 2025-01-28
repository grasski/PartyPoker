package com.dabi.partypoker

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.dabi.easylocalgame.payloadUtils.convertFromJsonToType
import com.dabi.easylocalgame.payloadUtils.fromClientPayload
import com.dabi.easylocalgame.payloadUtils.gsonBuilder
import com.dabi.easylocalgame.payloadUtils.toClientPayload
import com.dabi.partypoker.ui.theme.PartyPokerTheme
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.*
import java.lang.reflect.Type


@Serializable
sealed class TestClass {
    @Serializable
    object Test1 : TestClass()

    @Serializable
    data class Test2(val value: String) : TestClass()

    @Serializable
    data class Test3(val id: Int) : TestClass()
}

class TestClassTypeAdapter(): JsonSerializer<TestClass>, JsonDeserializer<TestClass> {
    override fun serialize(
        src: TestClass,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        val jsonObject = JsonObject()
        when (src) {
            is TestClass.Test1 -> {
                jsonObject.addProperty("type", "Test1")
            }
            is TestClass.Test2 -> {
                jsonObject.addProperty("type", "Test2")
                jsonObject.addProperty("value", src.value)
            }
            is TestClass.Test3 -> {
                jsonObject.addProperty("type", "Test3")
                jsonObject.addProperty("id", src.id)
            }
        }
        return jsonObject
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): TestClass {
        val jsonObject = json.asJsonObject
        val type = jsonObject.get("type").asString
        return when (type) {
            "Test1" -> TestClass.Test1
            "Test2" -> TestClass.Test2(jsonObject.get("value").asString)
            "Test3" -> TestClass.Test3(jsonObject.get("id").asInt)
            else -> throw IllegalArgumentException("Unknown type: $type")
        }
    }

}

@Serializable
data class Nevim(
    var a: Int,
    var b: String,
    var t: TestClass
)

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())


        setContent {
            PartyPokerTheme {
                Scaffold {
                    Box(modifier = Modifier
                        .padding(it)
                        .fillMaxSize()
                    ){

//                        val testInstance = Nevim(
//                            1, "2",
//                            TestClass.Test3(6)
//                        )
////                        gsonBuilder.registerTypeAdapter(TestClass::class.java, TestClassTypeAdapter())
//                        val serialized = toClientPayload("test", testInstance, typeAdapters = mapOf(TestClass::class.java to TestClassTypeAdapter()))
//                        println(serialized)
//                        val deserialized = fromClientPayload<String, Nevim>(serialized, null)
//                        println(deserialized)
////                        println(deserialized.second?.convertFromJsonToType(Nevim::class.java))




                        Navigation()
                    }
                }
            }
        }
    }
}
