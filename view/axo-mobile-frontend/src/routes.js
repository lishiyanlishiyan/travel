import startsWith from 'lodash/startsWith'
import HomePage from './views/Home'
import AboutPage from './views/About'
import DevTools from './views/dev-tools'
import FeedbackPage from './views/feedback'
import NotFoundPage from './views/not-found'
import BookTravellers from './views/products/traveller/book-travellers'

import PanelLeftPage from './views/panel-left.vue'
import LoginPage from './views/login/Login.vue'
import DomAirRoutes from './routes/domair/DomAirRoutes'
import IntlAirRoutes from './routes/intlair/IntlAirRoutes'
import TrainRoutes from './routes/train/TrainRoutes'
import HotelRoutes from './routes/hotel/HotelRoutes'
import OrderRoutes from './routes/order/OrderRoutes'
import ProfileRoutes from './routes/profile/ProfileRoutes'
import ProductsRoutes from './routes/ProductsRoutes'
import NewsRoutes from './routes/news/NewsRoutes'

export default [
  {
    path: '/',
    component: HomePage,
    tabs: [
      {
        path: '/',
        id: 'index-products',
        async (routeTo, routeFrom, resolve, reject) {
          if (routeTo.path === '/') {
            const vueComponent = () => import('./views/index')
            vueComponent().then((vc) => {
              resolve({ component: vc.default })
            })
          }
        }
      },
      {
        path: '/continue-book/:taNo',
        id: 'index-products',
        async (routeTo, routeFrom, resolve, reject) {
          if (startsWith(routeTo.path, '/continue-book')) {
            const vueComponent = () => import('./views/index')
            vueComponent().then((vc) => {
              resolve({ component: vc.default })
            })
          }
        }
      },
      {
        path: '/order',
        id: 'index-order',
        async (routeTo, routeFrom, resolve, reject) {
          if (routeTo.path === '/order') {
            const vueComponent = () => import('./views/order/orders')
            vueComponent().then((vc) => {
              resolve({ component: vc.default })
            })
          }
        }
      },
      {
        path: '/profile',
        id: 'index-profile',
        async (routeTo, routeFrom, resolve, reject) {
          if (routeTo.path === '/profile') {
            const vueComponent = () => import('./views/profile/profile')
            vueComponent().then((vc) => {
              resolve({ component: vc.default })
            })
          }
        }
      }
    ]
  },
  {
    path: '/book-travellers/:productType',
    component: BookTravellers
  },
  {
    path: '/panel-left',
    component: PanelLeftPage
  },
  {
    path: '/login',
    component: LoginPage,
    options: {
      history: false
    }
  },
  {
    path: '/sso(.*)',
    component: LoginPage,
    options: {
      history: false
    }
  },
  {
    path: '/about',
    component: AboutPage
  },
  {
    path: '/dev',
    component: DevTools
  },
  {
    path: '/feedback',
    component: FeedbackPage
  },
  ...DomAirRoutes,
  ...IntlAirRoutes,
  ...TrainRoutes,
  ...HotelRoutes,
  ...OrderRoutes,
  ...ProfileRoutes,
  ...ProductsRoutes,
  ...NewsRoutes,
  {
    path: '(.*)',
    component: NotFoundPage
  }
]
