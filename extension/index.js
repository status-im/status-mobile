import React, { useState, useRef } from "react";
import {
  StyleSheet,
  Text,
  View,
  TextInput,
  TouchableOpacity,
  ScrollView
} from "react-native";
import Task from "./Task";

let statusAPI;

function Init (api) {statusAPI = api;}

function ExtensionView () {
  const [value, setValue] = useState("");
  const [todos, setTodos] = useState([]);
  const inp = useRef(null);

  const handleAddTodo = () => {
    if (value.length > 0) {
      setTodos([...todos, { text: value, key: Date.now(), checked: false }]);
      setValue("");
      inp.current.focus();
    }
  };

  const handleDeleteTodo = (id) => {
    setTodos(
      todos.filter((todo) => {
        if (todo.key !== id) return true;
      })
    );
    inp.current.focus();
  };

  const handleChecked = (id) => {
    setTodos(
      todos.map((todo) => {
        if (todo.key === id) todo.checked = !todo.checked;
        return todo;
      })
    );
  };

  const handleSend = () => {
    statusAPI.sendCommand(todos);
    statusAPI.close();
  };

  return (
    <View style={styles.container}>
      <View style={styles.textInputContainer}>
        <TextInput
          style={styles.textInput}
          autoFocus={true}
          multiline={true}
          onChangeText={(value) => setValue(value)}
          onSubmitEditing={(event) => handleAddTodo()}
          placeholder={"Todo"}
          placeholderTextColor="gray"
          ref={inp}
          value={value}
        />
        <TouchableOpacity onPress={() => handleAddTodo()}>
          <View style={{ marginLeft: 10, paddingHorizontal: 10}}>
            <Text style={{ fontSize: 30 }}>+</Text>
          </View>
        </TouchableOpacity>
      </View>
      <ScrollView style={{flex: 1}} keyboardShouldPersistTaps={"handled"}>
        {todos.map((task) => (
          <Task
            text={task.text}
            key={task.key}
            checked={task.checked}
            showDel={true}
            setChecked={() => handleChecked(task.key)}
            delete={() => handleDeleteTodo(task.key)}
          />
        ))}
      </ScrollView>
      <TouchableOpacity style={{alignSelf: "center"}} onPress={() => handleSend()}>
          <Text style={{ fontSize: 30 }}>Send</Text>
        </TouchableOpacity>
    </View>
  );
};

function MessageView (props) {
  const [todos, setTodos] = useState(props.params);

  const handleChecked = (id) => {
    setTodos(
      todos.map((todo) => {
      console.log("todo.key " +  todo.key);
        if (todo.key === id) todo.checked = !todo.checked;
        return todo;
      })
    );
  };

  return (
  <View style={{padding: 10, borderWidth: 1, borderColor: "#939ba1", borderRadius: 8}}
  key={props.id}>
  <Text>Todo</Text>
    <View style={{marginTop: 10}} >
        {todos.map((task) => (
           <View style={{flexDirection: "row", marginBottom: 15}} key={task.key}>
            <TouchableOpacity onPress={() => handleChecked(task.key)}>
                <View
                  style={{
                    borderColor: "gray",
                    borderWidth: 1,
                    width: 20,
                    height: 20,
                    alignItems: "center",
                    justifyContent: "center"
                  }}
                >
                  <Text>{task.checked ? "V" : ""}</Text>
                </View>
              </TouchableOpacity>
           <Text style={{width: 200, marginLeft: 10}}>{task.text}</Text>
           </View>
        ))}
      </View>
      </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20
  },
  textInput: {
    flex: 1,
    fontSize: 25,
    paddingLeft: 10,
    borderWidth: 1,
    borderColor: "gray"
  },
  taskWrapper: {
    marginTop: "5%",
    flexDirection: "row",
    // alignItems: 'baseline',
    borderColor: "#D0D0D0",
    borderBottomWidth: 0.5,
    width: "100%",
    alignItems: "stretch",
    minHeight: 40
  },
  task: {
    paddingBottom: 20,
    paddingLeft: 10,
    paddingTop: 6,
    borderBottomWidth: 1,
    fontSize: 17,
    fontWeight: "bold"
  },
  textInputContainer: {
    flexDirection: "row",
    justifyContent: "center",
    paddingBottom: 5,
    marginBottom: 10
  }
});

export default [{
    scope: ["PERSONAL_CHATS"],
    type: "CHAT_COMMAND",
    view: ExtensionView,
    messageView: MessageView,
    init: Init
}]