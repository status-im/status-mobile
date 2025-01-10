(ns status-im.contexts.wallet.add-account.create-account.new-keypair.confirm-backup.style)

(def buttons-container
  {:padding-horizontal 20
   :padding-vertical   12
   :position           :absolute
   :bottom             0
   :left               0
   :right              0})

(defn buttons-inner-container
  [margin-bottom]
  {:flex-direction :row
   :margin-bottom  margin-bottom})

(defn button
  [margin-right]
  {:flex         0.5
   :margin-right margin-right})

(def cheat-description
  {:padding-horizontal 20
   :padding-top        8
   :padding-bottom     8})
