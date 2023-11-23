(ns quo.components.wallet.confirmation-progress.style)

(defn progress-box-container
  [bottom-large?]
  {:flex-direction     :row
   :align-items        :center
   :padding-horizontal 12
   :padding-bottom     (if bottom-large? 12 8)
   :padding-top        4
   :flex-wrap          :wrap})
