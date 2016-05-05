(ns syng-im.components.chat.input.money
  (:require
   [re-frame.core :refer [subscribe dispatch dispatch-sync]]
   [syng-im.components.styles :refer [font]]
   [syng-im.components.chat.input.simple-command :refer [simple-command-input-view]]
   [syng-im.utils.utils :refer [log toast http-post]]
   [syng-im.utils.logging :as log]))

(defn money-input-view [command]
  [simple-command-input-view command {:keyboardType "numeric"
                                               :style {:flex       1
                                                       :marginLeft 8
                                                       :lineHeight 42
                                                       :fontSize   32
                                                       :fontFamily font
                                                       :color      "black"}}])
