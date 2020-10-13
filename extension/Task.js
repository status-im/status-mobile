import React from "react";
import { View, TouchableOpacity, Text, StyleSheet } from "react-native";
const Task = (props) => (
  <View style={styles.taskWrapper}>
    {props.showCheck &&
      <TouchableOpacity onPress={() => props.setChecked()}>
        <View
          style={{
            borderColor: "gray",
            borderWidth: 1,
            width: 30,
            height: 30,
            alignItems: "center",
            justifyContent: "center"
          }}
        >
          <Text>{props.checked ? "V" : ""}</Text>
        </View>
      </TouchableOpacity>}

    <View style={{ flex: 1 }}>
      {props.checked && <View style={styles.verticalLine}></View>}
      <Text style={styles.task}>{props.text}</Text>
    </View>

   {props.showDel &&
    <TouchableOpacity onPress={() => props.delete()}>
      <View style={{ marginLeft: 10, paddingHorizontal: 10 }}>
        <Text style={{ fontSize: 30 }}>-</Text>
      </View>
    </TouchableOpacity>}
  </View>
);

export default Task;

const styles = StyleSheet.create({
  taskWrapper: {
    flex: 1,
    flexDirection: "row",
    borderColor: "#FFFFFF",
    borderBottomWidth: 1.5,
    width: "100%",
    alignItems: "stretch",
    minHeight: 40
  },
  task: {
    paddingBottom: 20,
    paddingLeft: 10,
    marginTop: 6,
    borderColor: "#F0F0F0",
    borderBottomWidth: 1,
    fontSize: 17,
    fontWeight: "bold"
  },
  verticalLine: {
    borderBottomWidth: 1,
    marginLeft: 10,
    width: "100%",
    position: "absolute",
    marginTop: 15
  }
});
