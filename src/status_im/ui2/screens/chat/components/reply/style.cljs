(ns status-im.ui2.screens.chat.components.reply.style)

(defn reply-content
  [pin?]
  {:padding-horizontal (when-not pin? 10)
   :flex               1
   :flex-direction     :row})

(defn quoted-message
  [pin?]
  (merge {:flex-direction :row
          :align-items    :center
          :width          "45%"}
         (when-not pin?
           {:position :absolute
            :left     34
            :top      3})))
