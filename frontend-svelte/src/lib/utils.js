export function getBaseUrl() {
    console.log(import.meta.env.MODE)
    return (import.meta.env.PROD) ? "PRODURLWILLGOHERE" : "http://spring-boot:8080" 
}