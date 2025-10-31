'use client';

import { useEffect, useRef, useState } from 'react';
import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend } from 'chart.js';
import { Bar } from 'react-chartjs-2';

// Register ChartJS components
ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend);

const Dashboard = () => {
  const [currentDate, setCurrentDate] = useState('');

  useEffect(() => {
    // Set current date in the format "Friday, 17 October 2025"
    const date = new Date();
    const options: Intl.DateTimeFormatOptions = { 
      weekday: 'long', 
      day: 'numeric', 
      month: 'long', 
      year: 'numeric' 
    };
    setCurrentDate(date.toLocaleDateString('en-US', options));
  }, []);

  // Chart data
  const data = {
    labels: ['16 Oct', '18 Oct', '20 Oct', '22 Oct', '24 Oct', '26 Oct', '28 Oct'],
    datasets: [
      {
        label: 'PunchOut Sessions',
        data: [135, 102, 185, 125, 112, 130, 112],
        backgroundColor: '#2c3e50',
      },
      {
        label: 'Order Requests',
        data: [85, 75, 105, 80, 75, 70, 85],
        backgroundColor: '#3498db',
      },
      {
        label: 'Order Notices',
        data: [65, 70, 65, 60, 65, 80, 60],
        backgroundColor: '#5dade2',
      },
    ],
  };

  // Chart options
  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom' as const,
      },
      title: {
        display: true,
        text: 'Layered Overview of All Services',
        font: {
          size: 16,
        },
      },
    },
    scales: {
      x: {
        type: 'category' as const,
        grid: {
          display: true,
          color: 'rgba(0, 0, 0, 0.05)',
        },
      },
      y: {
        type: 'linear' as const,
        grid: {
          display: true,
          color: 'rgba(0, 0, 0, 0.05)',
        },
        ticks: {
          beginAtZero: true,
        },
        title: {
          display: true,
          text: 'Number of Records',
        },
      },
    },
  };

  return (
    <div className="dashboard-container p-4">
      <div className="card shadow-sm mb-4">
        <div className="card-body">
          <div className="d-flex justify-content-between align-items-center mb-3">
            <h5 className="card-title">Dashboard Overview</h5>
            <div className="text-muted">{currentDate}</div>
          </div>
          <div className="chart-container" style={{ height: '400px' }}>
            <Bar data={data} options={options} />
          </div>
        </div>
      </div>

      <div className="row">
        <div className="col-md-4 mb-4">
          <div className="card shadow-sm h-100">
            <div className="card-body">
              <h5 className="card-title">PunchOut Sessions</h5>
              <h2 className="display-4 mb-3 text-primary">185</h2>
              <p className="text-muted">Total sessions this week</p>
              <div className="progress">
                <div 
                  className="progress-bar bg-primary" 
                  role="progressbar" 
                  style={{ width: '75%' }} 
                  aria-valuenow={75} 
                  aria-valuemin={0} 
                  aria-valuemax={100}
                ></div>
              </div>
            </div>
          </div>
        </div>

        <div className="col-md-4 mb-4">
          <div className="card shadow-sm h-100">
            <div className="card-body">
              <h5 className="card-title">Order Requests</h5>
              <h2 className="display-4 mb-3 text-info">105</h2>
              <p className="text-muted">Total orders this week</p>
              <div className="progress">
                <div 
                  className="progress-bar bg-info" 
                  role="progressbar" 
                  style={{ width: '60%' }} 
                  aria-valuenow={60} 
                  aria-valuemin={0} 
                  aria-valuemax={100}
                ></div>
              </div>
            </div>
          </div>
        </div>

        <div className="col-md-4 mb-4">
          <div className="card shadow-sm h-100">
            <div className="card-body">
              <h5 className="card-title">Order Notices</h5>
              <h2 className="display-4 mb-3 text-success">65</h2>
              <p className="text-muted">Total notices this week</p>
              <div className="progress">
                <div 
                  className="progress-bar bg-success" 
                  role="progressbar" 
                  style={{ width: '45%' }} 
                  aria-valuenow={45} 
                  aria-valuemin={0} 
                  aria-valuemax={100}
                ></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;