import React from 'react';

const Login = () => {
  const handleLogin = () => {
    // Redirect to the backend's Google OAuth initiation endpoint
    // This URL should match the one configured in Spring Security for OAuth2 login
    window.location.href = 'http://localhost:8080/oauth2/authorization/google';
  };

  return (
    <div style={{ textAlign: 'center', marginTop: '100px' }}>
      <h1>파일 확장자 차단 시스템</h1>
      <p>로그인하여 서비스를 이용해주세요.</p>
      <button 
        onClick={handleLogin}
        style={{
          padding: '10px 20px',
          fontSize: '16px',
          cursor: 'pointer',
          backgroundColor: '#4285F4', // Google blue
          color: 'white',
          border: 'none',
          borderRadius: '5px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          margin: '20px auto',
          boxShadow: '0 2px 4px rgba(0,0,0,0.2)'
        }}
      >
        <img 
          src="https://upload.wikimedia.org/wikipedia/commons/5/53/Google_%22G%22_Logo.svg" 
          alt="Google logo" 
          style={{ width: '20px', height: '20px', marginRight: '10px' }} 
        />
        Google 계정으로 로그인
      </button>
    </div>
  );
};

export default Login;
