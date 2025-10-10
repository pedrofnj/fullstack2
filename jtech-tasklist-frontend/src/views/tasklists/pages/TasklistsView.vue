<template>
  <v-container>
    <v-row class="align-center mb-4">
      <v-col cols="8"><h2>Minhas listas</h2></v-col>
      <v-col cols="4" class="text-right">
        <v-btn color="primary" @click="showCreateDialog = true">Nova Lista</v-btn>
      </v-col>
    </v-row>

    <v-row>
      <v-col cols="12" sm="6" md="4" v-for="l in lists.items" :key="l.id">
        <v-card class="pa-4" @click="goToTasks(l.id)" style="cursor: pointer;">
          <v-card-title>{{ l.name }}</v-card-title>
          <v-card-actions>
            <v-btn size="small" variant="text" @click.stop="rename(l)">Renomear</v-btn>
            <v-spacer></v-spacer>
            <v-btn size="small" color="error" variant="text" @click.stop="remove(l.id)"
              >Excluir</v-btn
            >
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>

    <v-dialog v-model="showCreateDialog" max-width="500px">
      <v-card>
        <v-card-title>Criar Nova Lista</v-card-title>
        <v-card-text>
          <v-text-field
            v-model="newName"
            label="Nome da Lista"
            :rules="[(v) => !!v || 'Nome é obrigatório']"
            required
          />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="showCreateDialog = false">Cancelar</v-btn>
          <v-btn color="primary" @click="create">Salvar</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="showCannotDeleteDialog" max-width="400px">
      <v-card>
        <v-card-title class="text-h6">Não é possível excluir</v-card-title>
        <v-card-text>Não é possível excluir uma lista que possui tarefas vinculadas.</v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn color="primary" @click="showCannotDeleteDialog = false">OK</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="showDeleteDialog" max-width="400px">
      <v-card>
        <v-card-title class="text-h6">Confirmar Exclusão</v-card-title>
        <v-card-text>Tem certeza que deseja excluir esta lista?</v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="showDeleteDialog = false">Cancelar</v-btn>
          <v-btn color="error" @click="confirmDelete">Excluir</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="showRenameDialog" max-width="500px">
      <v-card>
        <v-card-title>Renomear Lista</v-card-title>
        <v-card-text>
          <v-text-field
            v-model="renameName"
            label="Novo nome da lista"
            :rules="[(v) => !!v || 'Nome é obrigatório']"
            required
          />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="showRenameDialog = false">Cancelar</v-btn>
          <v-btn color="primary" @click="renameListName">Salvar</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useTasklistsStore } from '@/stores/useTasklistsStore'
import { useUiStore } from '@/stores/useUiStore'
import { Services } from '@/core/services'
import { useRouter } from 'vue-router'
import type { Tasklist } from '@/core/models/tasklist'

const lists = useTasklistsStore()
const ui = useUiStore()
const router = useRouter()
const newName = ref('')
const showCreateDialog = ref(false)
const showCannotDeleteDialog = ref(false)
const showDeleteDialog = ref(false)
const overdueByListId = ref<Record<string, number>>({})
const listIdToDelete = ref<string | null>(null)
const showRenameDialog = ref(false)
const renameList = ref<Tasklist | null>(null)
const renameName = ref('')

onMounted(async () => {
  await lists.fetch()
  const now = new Date()
  const result: Record<string, number> = {}
  for (const l of lists.items) {
    const tasks = await Services.tasks.listByListId(l.id)
    result[l.id] = tasks.filter(
      (t: { completed: boolean; dueDate?: string }) =>
        !t.completed && t.dueDate && new Date(t.dueDate) < now,
    ).length
  }
  overdueByListId.value = result
})

const create = async () => {
  const name = newName.value.trim()
  if (!name) return
  if (lists.items.some((l) => l.name.toLowerCase() === name.toLowerCase())) {
    ui.showSnackbar('Já existe uma lista com esse nome.', 'error')
    return
  }
  await lists.create(name)
  ui.showSnackbar('Lista criada com sucesso!', 'success')
  newName.value = ''
  showCreateDialog.value = false
}
const rename = async (l: { id: string; name: string }) => {
  renameList.value = l
  renameName.value = l.name
  showRenameDialog.value = true
}
const renameListName = async () => {
  if (renameList.value && renameName.value.trim()) {
    const trimmedName = renameName.value.trim()
    if (
      lists.items.some(
        (list) => list.id !== renameList.value.id && list.name.toLowerCase() === trimmedName.toLowerCase(),
      )
    ) {
      ui.showSnackbar('Já existe uma lista com esse nome.', 'error')
      return
    }
    await lists.rename(renameList.value.id, trimmedName)
    ui.showSnackbar('Lista renomeada com sucesso!', 'success')
    showRenameDialog.value = false
  }
}
const remove = async (id: string) => {
  const tasks = await Services.tasks.listByListId(id)
  if (tasks.length > 0) {
    showCannotDeleteDialog.value = true
    return
  }
  listIdToDelete.value = id
  showDeleteDialog.value = true
}
const confirmDelete = async () => {
  if (listIdToDelete.value) {
    await lists.remove(listIdToDelete.value)
    ui.showSnackbar('Lista excluída com sucesso!', 'success')
    listIdToDelete.value = null
    showDeleteDialog.value = false
  }
}
const goToTasks = (listId: string) => {
  router.push({ name: 'tasks', params: { listId } })
}
</script>
