(ns react-native.syntax-highlighter
  (:require ["react-syntax-highlighter" :default Highlighter]
            [reagent.core :as reagent]))

(def highlighter (reagent/adapt-react-class Highlighter))