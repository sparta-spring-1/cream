import { Link } from 'react-router-dom';
import { Search, Bell, Database, LogOut } from 'lucide-react';
import { authApi } from '../../api/auth';

const Header = () => {
    const isLoggedIn = !!localStorage.getItem('accessToken');

    const handleLogout = async () => {
        if (!confirm("로그아웃 하시겠습니까?")) return;
        try {
            // Attempt to call backend logout, but verify token exists first
            await authApi.logout();
        } catch (error) {
            console.error("Logout failed", error);
        } finally {
            // Always clear local state and redirect
            localStorage.removeItem('accessToken');
            window.location.href = '/';
        }
    };

    return (
        <div className="w-full bg-white border-b border-gray-200 sticky top-0 z-50">
            <header className="max-w-content mx-auto flex items-center justify-between px-10 py-4 gap-8">
                <div className="flex items-center gap-10 flex-1">
                    <Link to="/" className="flex items-center gap-2 cursor-pointer">
                        <Database className="text-primary" size={32} strokeWidth={2.5} />
                        <h2 className="text-2xl font-black tracking-tighter">CREAM</h2>
                    </Link>

                    <div className="flex-1 max-w-lg relative">
                        <span className="absolute inset-y-0 left-0 flex items-center pl-3">
                            <Search className="text-gray-400" size={20} />
                        </span>
                        <input
                            className="w-full bg-gray-100 border-none rounded-lg py-2.5 pl-10 pr-4 text-sm focus:ring-2 focus:ring-primary/20 placeholder:text-gray-500"
                            placeholder="브랜드, 상품, 프로필, 태그 등"
                            type="text"
                        />
                    </div>
                </div>

                <div className="flex items-center gap-8">
                    <nav className="flex items-center gap-8">
                        <Link to="/products" className="text-sm font-semibold hover:text-primary transition-colors">쇼핑</Link>
                        {isLoggedIn && (
                            <Link to="/me" className="text-sm font-semibold hover:text-primary transition-colors">마이페이지</Link>
                        )}
                    </nav>

                    <div className="flex gap-4 border-l border-gray-200 pl-8">
                        {isLoggedIn ? (
                            <>
                                <Link to="/notifications" className="flex items-center justify-center w-10 h-10 rounded-full hover:bg-gray-100 transition-colors">
                                    <Bell size={24} className="text-gray-900" />
                                </Link>
                                <button
                                    onClick={handleLogout}
                                    className="flex items-center justify-center w-10 h-10 rounded-full hover:bg-gray-100 transition-colors cursor-pointer border-none bg-transparent"
                                    title="로그아웃"
                                >
                                    <LogOut size={24} className="text-gray-900" />
                                </button>
                            </>
                        ) : (
                            <Link to="/auth" className="text-sm font-semibold hover:text-primary transition-colors">로그인</Link>
                        )}
                    </div>
                </div>
            </header>
        </div>
    );
};

export default Header;
