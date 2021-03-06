﻿using Serie3;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace Ex3GUI
{
    public partial class Form1 : Form
    {

        CancellationTokenSource ct;
        public Form1()
        {
            InitializeComponent();
            this.ct = new CancellationTokenSource();
        }

        private void label1_Click(object sender, EventArgs e)
        {

        }

        private void label2_Click(object sender, EventArgs e)
        {

        }

        delegate void SetTextCallback(int value);

        private void SetValue(int value)
        {
            // InvokeRequired required compares the thread ID of the
            // calling thread to the thread ID of the creating thread.
            // If these threads are different, it returns true.
            Console.WriteLine("Set Value!");
            if (this.InvokeRequired)
            {
                Console.WriteLine("Needs to switch");
                SetTextCallback d = new SetTextCallback(SetValue);
                this.Invoke(d, new object[] { value });
            }
            else
            {
                Console.WriteLine("Doesnt need to switch");
                this.progressTask.Value = value;
            }
        }

        private async void  button1_Click(object sender, EventArgs e)
        {
            listView1.Items.Clear();
            this.ct = new CancellationTokenSource();

            string path = Dir.Text;
            int numberOfFiles = 1;

            if (!Directory.Exists(path))
            {
                listView1.Items.Add(new ListViewItem { Text = "Invalid Directory" });
                return;
            }

            try
            {
             numberOfFiles = Int32.Parse(NumberOfFIles.Text);
            }
            catch (Exception)
            {
                listView1.Items.Clear();
                ListViewItem toAdd = new ListViewItem();
                toAdd.Text = "Invalid number.";
                listView1.Items.Add(toAdd);
                return;
            }


            this.progressTask.Value = 0;
            listView1.Items.Add(new ListViewItem { Text = "Running... " });
            try {
                ResultParallel res = await Ex2.processDirAsync(path, numberOfFiles, ct.Token, (progress, totalItems) => { SetValue((Int32)((((float)(int)progress)/(float)(int)totalItems)* 100f)); });
                listView1.Items.Clear();
                listView1.Items.Add(new ListViewItem { Text = "Number of files processed " + res.numberOfFiles });
                listView1.Items.AddRange(res.list.ConvertAll<ListViewItem>((item) =>
                {
                    return new ListViewItem
                    {
                        Text = item
                    };
                }).ToArray());
            }
            catch (OperationCanceledException ex)
            {
                listView1.Items.Clear();
                listView1.Items.Add(new ListViewItem { Text = "Cancelled...." });
            }

            catch (Exception ex)
            {
                listView1.Items.Clear();
                listView1.Items.Add(new ListViewItem { Text = "Error: " + ex.Message });
            }
            finally
            {
                this.ct.Dispose();
            }
            }

        private void button2_Click(object sender, EventArgs e)
        {
            // cancel
            try {
                ct.Cancel();
            } catch { }
                
        }

        private void contextMenuStrip1_Opening(object sender, CancelEventArgs e)
        {

        }

        private void Dir_TextChanged(object sender, EventArgs e)
        {

        }
    }
}
