# SmartAlert

## Project Overview  
This is a **university project**. The app is designed to **alert users about nearby natural disasters**.  

- **All users** can send an **alert request**, providing the **type of disaster**, the **location**, and a **photo**.  
- **Civil protection employees** can view a **list of pending requests**, sorted using a **smart point system**.  
  - When multiple requests of the **same type** are in **close proximity**, they are **merged into a single request** and given more points.  
  - This ensures that the most **"popular"** alerts appear at the top of the list.  
- Employees can **accept or decline requests**.  
  - Upon **acceptance**, **all users in the affected area** receive an alert on their **phones** with the appropriate **instructions** for the event, in their phone **current language**.  

The app also features a **statistics page** for tracking disaster reports and responses.  

## Technology Stack  
- **Android Studio** – Primary development environment.  
- **Java** – Main programming language for the app.  
- **Firebase Firestore / Realtime Database** – For storing and syncing alert requests.  
- **Google Maps API** – For location-based alert requests and disaster mapping.
