(ns status-im2.contexts.chat.composer.reply.style
  (:require [react-native.platform :as platform]))


(defn container
  [pin? in-chat-input?]
  {:flex-direction :row
   :height         (when-not pin? 24)
   :margin-left    (if (and (not in-chat-input?) (not pin?)) 26 (if platform/android? 4 0))
   :margin-bottom  (when (and (not in-chat-input?) (not pin?)) 8)})
(defn reply-content
  [pin?]
  {:padding-right  (when-not pin? 10)
   :flex           1
   :flex-direction :row})

(defn quoted-message
  [pin?]
  (merge {:flex-direction :row
          :flex           1
          :align-items    :center}
         (when-not pin?
           {:left         22
            :margin-right 22})))

(def reply-from
  {:flex-direction :row
   :align-items    :center})

(def message-author-text
  {:margin-left 4})

(def message-text
  {:text-transform :none
   :margin-left    4
   :margin-top     2
   :flex-shrink    1})

(def gradient
  {:position :absolute
   :right    0
   :top      0
   :bottom   0
   :width    "50%"})

(def reply-deleted-message
  {:text-transform :none
   :margin-left    4
   :margin-top     2})
