import './App.css'
import './index.css';
import Login from './Components/Login';
import { Navigate, Routes , Route, BrowserRouter } from 'react-router-dom';
import Dashboard from './Components/Dashboard';
import ProtectedRoute from './Components/ProtectedRoute';
import ArchivedNotes from './Components/ArchivedNotes';


function App() {


  return (
    <>
     <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login/>} />
        <Route path="/dashboard" 
        element={
        <ProtectedRoute>
          <Dashboard/>
        </ProtectedRoute>
      }/>
      <Route path="/archived" 
        element={
        <ProtectedRoute>
          <ArchivedNotes/>
        </ProtectedRoute>
      }/>
        <Route path="" element={<Navigate to="/dashboard"/>} />
        
        
      </Routes>
     </BrowserRouter>
    </>
  )
}

export default App
