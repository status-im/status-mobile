(ns status-im.contexts.communities.actions.community-rules.style)

(defn community-rule
  [rules-bottom-space]
  {:flex               1
   :align-items        :flex-start
   :padding-top        8
   :margin-bottom      rules-bottom-space
   :padding-horizontal 20})
