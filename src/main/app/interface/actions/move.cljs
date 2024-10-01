(ns app.interface.actions.move)

(def move-action
  {:start-tile 0
   :end-tile 1
   :action-point-cost 2})

(defn execute-move-action
  [move-action gridmap])
