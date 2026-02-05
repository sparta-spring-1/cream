import { Outlet } from 'react-router-dom';
import Header from './Header';

const Layout = () => {
    return (
        <div className="layout-container min-h-screen">
            <Header />
            <main className="w-full">
                <Outlet />
            </main>
        </div>
    );
};

export default Layout;
