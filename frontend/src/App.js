import React, { useState, useEffect } from 'react';
import FixedExtensions from './components/FixedExtensions';
import ExtensionInput from './components/ExtensionInput';
import CustomExtensions from './components/CustomExtensions';
import FileExtensionChecker from './components/FileExtensionChecker'; // Import FileExtensionChecker
import Login from './components/Login'; // Import the new Login component
import { authApi } from './services/api'; // Import authApi
import './App.css'; // Keep App.css for basic app styling if needed, or remove if all styling is in index.css

function App() {
  const [customExtensionsRefreshTrigger, setCustomExtensionsRefreshTrigger] = useState(0);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loadingAuth, setLoadingAuth] = useState(true);

  useEffect(() => {
    const checkAuthentication = async () => {
      try {
        await authApi.checkAuthStatus();
        setIsAuthenticated(true);
      } catch (error) {
        setIsAuthenticated(false);
        // No need to alert here, the interceptor in api.js will handle 401 if it's an API call
        // For initial load, if not authenticated, we just show the login page.
      } finally {
        setLoadingAuth(false);
      }
    };

    checkAuthentication();
  }, []);

  const handleCustomExtensionAdded = () => {
    setCustomExtensionsRefreshTrigger(prev => prev + 1);
  };

  if (loadingAuth) {
    return <div style={{ textAlign: 'center', marginTop: '50px' }}>인증 상태 확인 중...</div>;
  }

  if (!isAuthenticated) {
    return <Login />;
  }

  return (
    <div className="App" style={{ maxWidth: '800px', margin: '40px auto', padding: '20px', border: '1px solid #eee', borderRadius: '10px', boxShadow: '0 4px 8px rgba(0,0,0,0.05)', fontFamily: 'Arial, sans-serif' }}>
      <h1 style={{ textAlign: 'center', color: '#333', marginBottom: '30px' }}>파일 확장자 차단 시스템</h1>
      
      <FixedExtensions />
      
      <div style={{ marginBottom: '20px' }}>
        <h2 style={{ fontSize: '1.2em', marginBottom: '10px' }}>커스텀 확장자 추가</h2>
        <ExtensionInput onAdd={handleCustomExtensionAdded} />
      </div>

      <CustomExtensions refreshTrigger={customExtensionsRefreshTrigger} />

      <FileExtensionChecker /> {/* Add FileExtensionChecker */}
    </div>
  );
}

export default App;
