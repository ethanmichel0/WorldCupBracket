import org.springframework.data.mongodb.repository.MongoRepository

interface GameRepository : MongoRepository<Game,String>{

}