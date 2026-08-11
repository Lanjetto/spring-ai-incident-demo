import { Routes, Route } from 'react-router-dom'
import BucketList from './components/BucketList.jsx'
import BucketDetail from './components/BucketDetail.jsx'
import IncidentDetail from './components/IncidentDetail.jsx'

export default function App() {
  return (
    <div className="app">
      <Routes>
        <Route path="/" element={<BucketList />} />
        <Route path="/bucket/:level" element={<BucketDetail />} />
        <Route path="/incident/:id" element={<IncidentDetail />} />
      </Routes>
    </div>
  )
}
