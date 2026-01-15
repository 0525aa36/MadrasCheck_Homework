import React, { useState, useEffect } from 'react';
import FixedExtensions from './components/FixedExtensions';
import ExtensionInput from './components/ExtensionInput';
import CustomExtensions from './components/CustomExtensions';
import FileExtensionChecker from './components/FileExtensionChecker';
import ExtensionHistory from './components/ExtensionHistory';
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
        console.log('사용자 정보 응답:', response.data);
        // ApiResponse 구조: { success, message, data }
        const userData = response.data.data;
        if (userData) {
          setCurrentUser(userData);
          setIsAuthenticated(true);
        } else {
          setIsAuthenticated(false);
        }
      } catch (error) {
        console.error('인증 확인 실패:', error);
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

  const handleLogin = () => {
    const backendUrl = process.env.REACT_APP_BACKEND_URL || 'http://localhost:8080';
    window.location.href = `${backendUrl}/oauth2/authorization/google`;
  };

  const handleLogout = async () => {
    try {
      await authApi.logout();
      setIsAuthenticated(false);
      setCurrentUser(null);
    } catch (error) {
      console.error('로그아웃 실패:', error);
    }
  };

  if (loadingAuth) {
    return (
      <div className="loading-container">
        <div className="loading-spinner"></div>
        <p>인증 상태 확인 중...</p>
      </div>
    );
  }

  return (
    <div className="app-container">
      <div className="app-header">
        <div className="header-content">
          <h1 className="app-title">
            파일 확장자 차단 시스템
          </h1>
          {isAuthenticated && currentUser ? (
            <div className="user-info">
              {currentUser.picture ? (
                <img 
                  src={currentUser.picture} 
                  alt={currentUser.name}
                  className="user-avatar"
                />
              ) : (
                <div className="user-avatar">
                  {currentUser.name ? currentUser.name[0].toUpperCase() : '?'}
                </div>
              )}
              <span className="user-name">{currentUser.name}</span>
              <button className="logout-btn" onClick={handleLogout}>
                로그아웃
              </button>
            </div>
          ) : (
            <button className="login-btn" onClick={handleLogin}>
              로그인
            </button>
          )}
        </div>
      </div>

      <div className="app-content">
        <div className="main-section">
          <FixedExtensions 
            onUpdate={handleExtensionUpdate} 
            isAuthenticated={isAuthenticated}
            onLoginRequired={handleLogin}
          />
          
          <div className="custom-section">
            {isAuthenticated && <ExtensionInput onAdd={handleCustomExtensionAdded} />}
            <CustomExtensions 
              refreshTrigger={customExtensionsRefreshTrigger} 
              onUpdate={handleExtensionUpdate}
              isAuthenticated={isAuthenticated}
              onLoginRequired={handleLogin}
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
