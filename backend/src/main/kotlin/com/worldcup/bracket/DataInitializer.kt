import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.springframework.boot.ApplicationArguments
import java.nio.file.Paths
import com.google.gson.Gson; 
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken
import com.worldcup.bracket.Entity.Group

import java.io.File

@Component
class DataInitializer : ApplicationRunner {
    public override fun run (args: ApplicationArguments) {
        val path: String = Paths.get("").toAbsolutePath().toString()
        val jsonString: String = File(path + "/src/constants.json").readText(Charsets.UTF_8)
        print(jsonString)
        val groups : GroupsWrapper = Gson().fromJson(jsonString, GroupsWrapper::class.java)
        println(groups);
        println("testtttt")
    }
}

data class GroupsWrapper(
    val groups: List<Group>
)