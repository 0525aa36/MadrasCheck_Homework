import React, { useState, useEffect } from 'react';
import FixedExtensions from './components/FixedExtensions';
import ExtensionInput from './components/ExtensionInput';
import CustomExtensions from './components/CustomExtensions';
import FileExtensionChecker from './components/FileExtensionChecker';
import ExtensionHistory from './components/ExtensionHistory';
import Login from './components/Login';
import { authApi } from './services/api';
import './App.css';

function App() {
  const [customExtensionsRefreshTrigger, setCustomExtensionsRefreshTrigger] = useState(0);
  const [historyRefreshTrigger, setHistoryRefreshTrigger] = useState(0);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loadingAuth, setLoadingAuth] = useState(true);
  const [currentUser, setCurrentUser] = useState(null);

  useEffect(() => {
    const checkAuthentication = async () => {
      try {
        const response = await authApi.checkAuthStatus();
        setCurrentUser(response.data);
        setIsAuthenticated(true);
      } catch (error) {
        setIsAuthenticated(false);
      } finally {
        setLoadingAuth(false);
      }
    };

    checkAuthentication();
  }, []);

  const handleCustomExtensionAdded = () => {
    setCustomExtensionsRefreshTrigger(prev => prev + 1);
    setHistoryRefreshTrigger(prev => prev + 1);
  };

  const handleExtensionUpdate = () => {
    setHistoryRefreshTrigger(prev => prev + 1);
  };

  if (loadingAuth) {
    return (
      <div className="loading-container">
        <div className="loading-spinner"></div>
        <p>인증 상태 확인 중...</p>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Login />;
  }

  return (
    <div className="app-container">
      <div className="app-header">
        <div className="header-content">
          <h1 className="app-title">
            파일 확장자 차단 시스템
          </h1>
          {currentUser && (
            <div className="user-info">
              <div className="user-avatar">
                {currentUser.name ? currentUser.name[0] : '?'}
              </div>
              <span className="user-name">{currentUser.name}</span>
            </div>
          )}
        </div>
      </div>

      <div className="app-content">
        <div className="main-section">
          <FixedExtensions onUpdate={handleExtensionUpdate} />
          
          <div className="custom-section">
            <ExtensionInput onAdd={handleCustomExtensionAdded} />
            <CustomExtensions 
              refreshTrigger={customExtensionsRefreshTrigger} 
              onUpdate={handleExtensionUpdate}
            />
          </div>

          <FileExtensionChecker />
        </div>

        <div className="sidebar-section">
          <ExtensionHistory key={historyRefreshTrigger} />
        </div>
      </div>
    </div>
  );
}

export default App;
