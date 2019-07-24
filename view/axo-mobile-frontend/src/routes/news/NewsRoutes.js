/**
 * @author gary.fu
 */
import NewsPage from '../../views/news/news'
import NewsDetailPage from '../../views/news/news-detail'

export default [{
  path: '/news',
  component: NewsPage
}, {
  path: '/news/newsDetail/:id',
  component: NewsDetailPage
}]
