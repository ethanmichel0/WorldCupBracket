import org.springframework.data.mongodb.repository.MongoRepository

interface TeamRepository : MongoRepository<Team,String>{

}