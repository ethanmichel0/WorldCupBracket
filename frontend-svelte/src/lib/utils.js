import { moveSyntheticComments } from "typescript"

export function getBaseUrlFromServer() {
    return (import.meta.env.PROD) ? "PRODURLWILLGOHERE" : "http://spring-boot:8080" 
}

export function getBaseUrlFromClient() {
    return (import.meta.env.PROD) ? "PRODURLWILLGOHERE" : "http://localhost:6868" 
    // see docker-compose.yml
}

// check if user is logged in, and if so set userinfo in store if it hasn't been set yet
export async function isUserLoggedIn(event) {
    console.log("in is userlogged in")
    let response = await fetch(`${getBaseUrlFromServer()}/api/userinfo`,
    {headers:{
        Cookie : `JSESSIONID=${event.cookies.get('JSESSIONID')}`
    }})
    if (response.status == 403) {
        event.locals.user = {
            name:"",
            email:"",
            id:""
        }
        return false
    }
    let body = await response.json()
    event.locals.user = {
        name:body.name,
        email:body.email,
        id:body.id
    }
    return true
}

export function formDataToJson(formData,allMultiSelectFields) {
    let object = {};
    formData.forEach((value, key) => {
        if (allMultiSelectFields.includes(key)) {
            if (Reflect.has(object, key)) object[key].push(value)
            else object[key] = [value]
            return
        }
        object[key] = value
      });
    let json = JSON.stringify(object);
    return json
}