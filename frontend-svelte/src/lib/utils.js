export function getBaseUrlFromServer() {
    return (import.meta.env.PROD) ? "PRODURLWILLGOHERE" : "http://spring-boot:8080" 
}

export function getBaseUrlFromClient() {
    return (import.meta.env.PROD) ? "PRODURLWILLGOHERE" : "http://localhost:6868" 
    // see docker-compose.yml
}

export async function isUserLoggedIn(cookies) {
    let response = await fetch(`${getBaseUrlFromServer()}/api/draftgroups`,
    {headers:{
        Cookie : `JSESSIONID=${cookies.get('JSESSIONID')}`
    }})
    return response.headers.get("content-type") == "application/json"
    // if spring boot is redirecting user to sign in, they are not currently authenticated
}

export function formDataToJson(formData,allMultiSelectFields) {
    console.log("in util method w var = " + allMultiSelectFields)
    let object = {};
    formData.forEach((value, key) => {
        if (allMultiSelectFields.includes(key)) {
            console.log(key + " is included in multiselect!")
            if (Reflect.has(object, key)) object[key].push(value)
            else object[key] = [value]
            return
        }
        object[key] = value
      });
    let json = JSON.stringify(object);
    console.log("complete json is: " + json)
    return json
}