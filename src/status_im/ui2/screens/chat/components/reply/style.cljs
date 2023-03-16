(ns status-im.ui2.screens.chat.components.reply.style)

(defn reply-content
  [pin?]
  {:padding-horizontal (when-not pin? 10)
   :flex               1
   :flex-direction     :row})

(defn quoted-message
  [pin? in-chat-input?]
  (merge {:flex-direction :row
          :align-items    :center
          :width          (if in-chat-input? "80%" "45%")}
         (when-not pin?
           {:position :absolute
            :left     34
            :top      3})))

(def gradient
  {:position :absolute
   :right    0
   :top      0
   :bottom   0
   :width    "50%"})
