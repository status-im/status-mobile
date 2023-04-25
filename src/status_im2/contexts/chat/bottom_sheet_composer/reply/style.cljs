(ns status-im2.contexts.chat.bottom-sheet-composer.reply.style)

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
   :flex           1})

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


