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
        <v-card class="pa-4" :to="{ name: 'tasks', params: { listId: l.id } }" style="cursor: pointer;">
          <v-card-title>{{ l.name }}</v-card-title>
          <v-card-actions>
            <v-btn size="small" variant="text" @click.stop="rename(l)">Renomear</v-btn>
            <v-spacer></v-spacer>
            <v-btn size="small" color="error" variant="text" @click.stop="remove(l.id)">Excluir</v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>

    <v-dialog v-model="showCreateDialog" max-width="500px">
      <v-card>
        <v-card-title>Criar Nova Lista</v-card-title>
        <v-card-text>
          <v-text-field v-model="newName" label="Nome da Lista" :rules="[v => !!v || 'Nome é obrigatório']" required />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="showCreateDialog = false">Cancelar</v-btn>
          <v-btn color="primary" @click="create">Salvar</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useTasklistsStore } from '@/stores/useTasklistsStore'
import { useUiStore } from '@/stores/useUiStore'

const lists = useTasklistsStore()
const ui = useUiStore()
const newName = ref('')
const showCreateDialog = ref(false)

onMounted(lists.fetch)

const create = async () => {
  const name = newName.value.trim()
  if (!name) return
  if (lists.items.some(l => l.name.toLowerCase() === name.toLowerCase())) {
    ui.showSnackbar('Já existe uma lista com esse nome.', 'error')
    return
  }
  await lists.create(name)
  ui.showSnackbar('Lista criada com sucesso!', 'success')
  newName.value = ''
  showCreateDialog.value = false
}
const rename = async (l: { id: string; name: string }) => {
  const name = prompt('Novo nome:', l.name)
  if (name && name.trim() && name !== l.name) {
    const trimmedName = name.trim()
    if (lists.items.some(list => list.id !== l.id && list.name.toLowerCase() === trimmedName.toLowerCase())) {
      ui.showSnackbar('Já existe uma lista com esse nome.', 'error')
      return
    }
    await lists.rename(l.id, trimmedName)
  }
}
const remove = async (id: string) => {
  if (confirm('Excluir esta lista e suas tarefas?')) await lists.remove(id)
}
</script>
