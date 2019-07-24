/**
 * @author kang.wang
 */
import TrainSearchResultPage from '../../views/products/train/train-search-result'
import TrainSearchLastResultPage from '../../views/products/train/train-search-last-result'
import BookTrainSeatPage from '../../views/products/train/train-book'

export default [{
  path: '/train/doSsearchTrainSchedule',
  component: TrainSearchResultPage
}, {
  path: '/train/doSsearchLastTrainSchedule',
  component: TrainSearchLastResultPage
}, {
  path: '/train/toBookTrainSeat',
  component: BookTrainSeatPage
}]
