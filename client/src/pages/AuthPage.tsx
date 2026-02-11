import { useState } from 'react';
import { authApi } from '../api/auth';
import type { LoginRequest, SignupRequest } from '../types/auth';

const AuthPage = () => {
    const [isLogin, setIsLogin] = useState(true);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    // Form States
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [name, setName] = useState('');
    const [phoneNumber, setPhoneNumber] = useState('');

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setLoading(true);

        try {
            if (isLogin) {
                const payload: LoginRequest = { email, password };
                const response = await authApi.login(payload);
                localStorage.setItem('accessToken', response.data.accessToken);
                window.location.href = '/'; // Reload to refresh auth state/layout
            } else {
                const payload: SignupRequest = { email, password, name, phoneNumber };
                await authApi.signup(payload);
                alert('회원가입 성공! 로그인해주세요.');
                setIsLogin(true);
            }
        } catch (err: any) {
            console.error(err);
            setError(err.response?.data?.message || '오류가 발생했습니다.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="flex flex-col items-center justify-center min-h-[80vh]">
            <div className="w-full max-w-md p-8 bg-white rounded-xl shadow-lg border border-gray-100">
                <h2 className="text-2xl font-bold text-center mb-8 italic">
                    {isLogin ? 'LOGIN' : 'SIGN UP'}
                </h2>

                {error && (
                    <div className="mb-4 p-3 bg-red-50 text-red-500 text-sm rounded-lg">
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label className="label">이메일 주소</label>
                        <input
                            type="email"
                            className="input-field"
                            placeholder="example@cream.co.kr"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                        />
                    </div>

                    <div>
                        <label className="label">비밀번호</label>
                        <input
                            type="password"
                            className="input-field"
                            placeholder="영문, 숫자 포함 8자 이상"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                    </div>

                    {!isLogin && (
                        <>
                            <div>
                                <label className="label">이름</label>
                                <input
                                    type="text"
                                    className="input-field"
                                    placeholder="홍길동"
                                    value={name}
                                    onChange={(e) => setName(e.target.value)}
                                    required
                                />
                            </div>
                            <div>
                                <label className="label">전화번호</label>
                                <input
                                    type="tel"
                                    className="input-field"
                                    placeholder="01012345678"
                                    value={phoneNumber}
                                    onChange={(e) => setPhoneNumber(e.target.value)}
                                    required
                                />
                            </div>
                        </>
                    )}

                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full btn btn-primary mt-6 !h-12 text-base"
                    >
                        {loading ? '처리중...' : (isLogin ? '로그인' : '회원가입')}
                    </button>
                </form>

                <div className="mt-6 text-center text-sm">
                    <span className="text-gray-500">
                        {isLogin ? '계정이 없으신가요?' : '이미 계정이 있으신가요?'}
                    </span>
                    <button
                        type="button"
                        className="ml-2 font-semibold underline"
                        onClick={() => setIsLogin(!isLogin)}
                    >
                        {isLogin ? '회원가입' : '로그인'}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default AuthPage;
