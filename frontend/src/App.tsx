import './App.css';
import TodoItemsContainer from './TodoItemsContainer/TodoItemsContainer';

const App = () => {
  return (
    <article className='content'>
      <div>
        <h1>TODO</h1>
        <TodoItemsContainer/>
      </div>
    </article>
  );
};

export default App;
