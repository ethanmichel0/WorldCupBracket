export function getBaseUrl() {
    return (import.meta.env.PROD) ? "PRODURLWILLGOHERE" : "http://spring-boot:8080" 
}