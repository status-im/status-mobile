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
    }
    inp.current.clear();
    inp.current.focus();
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
          multiline={false}
          onChangeText={(value) => setValue(value)}
          onSubmitEditing={(event) => handleAddTodo()}
          blurOnSubmit={false}
          placeholder={"Todo"}
          placeholderTextColor="gray"
          ref={inp}
        />
        <TouchableOpacity onPress={() => handleAddTodo()}>
          <View style={{ marginLeft: 10, paddingHorizontal: 10}}>
            <Text style={{ fontSize: 30, color: "#4360df" }}>+</Text>
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
          <Text style={{ fontSize: 24, marginTop: 20, color: "#4360df" }}>Send</Text>
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
  <View style={{padding: 20, backgroundColor:  (props.outgoing) ? "#4360df" : "", borderRadius: 20}}
  key={props.id}>
  <Text style={{color: (props.outgoing) ? "white" : "black", fontWeight: "bold"}}>Todo</Text>
    <View style={{marginTop: 10}} >
        {todos.map((task) => (
        <TouchableOpacity onPress={() => handleChecked(task.key)}>
           <View style={{flexDirection: "row", marginBottom: 15}} key={task.key}>

                <View
                  style={{
                    borderColor: (props.outgoing) ? "white" : "black",
                    borderWidth: 1,
                    width: 20,
                    height: 20,
                    alignItems: "center",
                    justifyContent: "center"
                  }}
                >
                  <Text style={{color: (props.outgoing) ? "white" : "black"}}>{task.checked ? "V" : ""}</Text>
                </View>

           <Text style={{width: 200, marginLeft: 10, color: (props.outgoing) ? "white" : "black"}}>{task.text}</Text>
           </View>
           </TouchableOpacity>
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
    fontSize: 20,
    paddingLeft: 10
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