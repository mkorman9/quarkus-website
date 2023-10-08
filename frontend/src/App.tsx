import './App.css';
import TodoItemsTable from "./TodoItemsTable/TodoItemsTable";

const App = () => {
  return (
    <article className='content'>
      <div>
        <h1>TODO</h1>
        <TodoItemsTable />
      </div>
    </article>
  );
};

export default App;
