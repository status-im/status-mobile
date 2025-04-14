(ns status-im.contexts.wallet.common.screen-base.create-or-edit-account.style)

(def account-avatar-container
  {:padding-horizontal 20
   :padding-top        12})

(def reaction-button-container
  {:position :absolute
   :bottom   0
   :left     76})

(defn title-input-container
  [error?]
  {:padding-horizontal 20
   :padding-top        12
   :padding-bottom     (if error? 8 16)})

(def divider-1
  {:margin-bottom 12})

(def section-container
  {:padding-horizontal 20
   :padding-bottom     4})

(def color-picker-container
  {:padding-vertical 8})

(def color-picker
  {:padding-horizontal 20})

(def divider-2
  {:margin-top    4
   :margin-bottom 12})
