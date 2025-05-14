(ns status-im.contexts.wallet.sheets.select-asset.style)

(defn container
  [window-height]
  {:flex    1
   :display :flex
   :height  window-height})

(def search-input-container
  {:padding-horizontal 20
   :padding-vertical   8})

(def subheader-container
  {:justify-content    :flex-start
   :align-items        :center
   :flex-direction     :row
   :flex-wrap          :wrap
   :gap                4
   :padding-horizontal 20
   :margin-bottom      8})
